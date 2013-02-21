package org.docx4j.openpackaging.parts.WordprocessingML;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage.CxCy;
import org.junit.Test;


public class CxCyTest {
    
    @Test
    public void shouldNotResizeSmaller() {
//        calculate(10, 10, 10, 10, 10, 10);
        calculate(10, 10, 20, 20, 10, 10);
        
        calculate(30, 10, 20, 20, 20, 7);
        calculate(10, 30, 20, 20, 7, 20);
        
        calculate(20, 20, 10, 10, 10, 10);
        
        calculate(20, 20, 30, 10, 10, 10);
        calculate(20, 20, 10, 30, 10, 10);

        calculate(10, 30, 30, 10, 3, 10);
    }
    private void calculate(double imageWidth, double imageHeight, int slideWidth, int slideHeight, double expectedWidth, double expectedHeight) {
        // given
        
        // when
        CxCy scale = CxCy.scaleToFit(imageWidth, imageHeight, slideWidth, slideHeight);
        
        // then
        assertThat(scale.getCx(), is((long)expectedWidth));
        assertThat(scale.getCy(), is((long)expectedHeight));
    }

}