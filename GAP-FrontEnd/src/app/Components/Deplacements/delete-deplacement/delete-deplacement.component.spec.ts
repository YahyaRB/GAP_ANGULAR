import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteDeplacementComponent } from './delete-deplacement.component';

describe('DeleteDeplacementComponent', () => {
  let component: DeleteDeplacementComponent;
  let fixture: ComponentFixture<DeleteDeplacementComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DeleteDeplacementComponent]
    });
    fixture = TestBed.createComponent(DeleteDeplacementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
